(ns github.core
 (:require
  org.httpkit.client
  taoensso.timbre
  cheshire.core
  api.core
  time.core
  github.data
  cemerick.url))

(def iso8601-> (partial time.core/format-> :date-time-no-ms))

(def endpoint->url (partial api.core/endpoint->url github.data/base-url))

(defn with-options
 [params options]
 (cond
  (-> params :method (= :post))
  (assoc params :body (cheshire.core/generate-string options))

  :else
  (update-in params [:query-params] merge options)))

(defn with-api-version-headers
 [params]
 (update-in params [:headers] merge {"Accept" "application/vnd.github.v3+json"}))

(defn with-auth-headers
 [params token]
 (update-in params [:query-params] merge {:access_token token}))

(defn response-ok?
 [response]
 (contains? #{200 201 202 203 204 205 206} (:status response)))

(defn response->data
 [response]
 (-> response
  :body
  (cheshire.core/parse-string true)))

(defn scrub-response
 [response]
 (-> response
  (assoc-in [:opts :query-params :access_token] "***HIDDEN***")))

(defn throw-bad-response!
 [response]
 (when-not (response-ok? response)
  (taoensso.timbre/error (scrub-response response))
  (throw (Exception. "Bad response from Github")))
 response)

(defn parse-link
 [link]
 (let [[_ url] (re-find #"<(.*)>" link)
       [_ rel] (re-find #"rel=\"(.*)\"" link)]
   [(keyword rel) url]))

(defn parse-links
 "Takes the content of the link header from a github resp, returns a map of links"
 [link-body]
 (->> (clojure.string/split link-body #",")
      (map parse-link)
      (into {})))

(defn response->last-page
 [response]
 {:post [(pos-int? %)]}
 (if-let [links (:link (:headers response))]
  (let [last-url (cemerick.url/url (:last (parse-links links)))]
   (Integer. ((:query last-url) "page")))
  1))

(defn with-page
 [params n]
 (update-in params [:query-params] merge {:page n}))

(defn -api!
 ([token endpoint] (-api! token endpoint {}))
 ([token endpoint options & params]
  (taoensso.timbre/debug "Requesting Github endpoint" endpoint)
  (let [params' (-> (apply hash-map params)
                 (api.core/with-url (endpoint->url endpoint))
                 (with-options options)
                 with-api-version-headers
                 (with-auth-headers token))
        request (org.httpkit.client/request params')]
   (let [last-page (response->last-page @request)]
    ; pagination...
    (flatten
     (map
      (comp response->data throw-bad-response! deref)
      (flatten
       (conj
        [request]
        (pmap
         #(org.httpkit.client/request (with-page params' %))
         (range 2 (inc last-page)))))))))))
(def api! (memoize -api!))
