(ns travis.core
 (:require
  travis.data
  time.core
  cheshire.core
  api.core
  org.httpkit.client
  cemerick.url))

(def endpoint->url (partial api.core/endpoint->url travis.data/base-url))

(def iso8601-> (partial time.core/format-> :date-time-no-ms))

(defn github-user+repo->travis-slug
 [user repo]
 (cemerick.url/url-encode (str user "/" repo)))

(defn with-api-version-headers
 [params]
 (update-in params [:headers] merge {"Travis-API-Version" "3"}))

(defn with-user-agent
 [params]
 (update-in params [:headers] merge {"User-Agent" "data-bot"}))

(defn with-auth-headers
 [params token]
 (update-in params [:headers] merge {"Authorization" (str "token " token)}))

(defn response-ok?
 [response]
 (= 200 (:status response)))

(defn response->data
 [response]
 (-> response
  :body
  (cheshire.core/parse-string true)))

(defn throw-bad-response!
 [response]
 (when-not (response-ok? response)
  (taoensso.timbre/error response)
  (throw (Exception. "Bad response from Travis")))
 response)

(defn response->last-page
 [response]
 (if-let [pagination ((keyword "@pagination") (response->data response))]
  (let [per-page (:limit pagination)
        total (:count pagination)
        pages (inc (quot total per-page))]
   pages)
  1))

(defn with-page
 [params n]
 (let [page-size 100]
  (update-in params [:query-params] merge {:offset (* n page-size) :limit page-size})))

(defn -api!
 ([token endpoint] (-api! token endpoint {}))
 ([token endpoint options & params]
  (taoensso.timbre/debug "Requesting Travis endpoint" endpoint)
  (let [params' (-> (apply hash-map params)
                 (api.core/with-url (endpoint->url endpoint))
                 with-api-version-headers
                 (with-page 0)
                 (with-auth-headers token))
        request (org.httpkit.client/request params')]
   ; pagination...
   (let [last-page (response->last-page @request)]
    (flatten
     (map
      (comp response->data throw-bad-response! deref)
      (flatten
       (conj
        [request]
        (pmap
         #(org.httpkit.client/request (with-page params' %))
         (range 1 last-page))))))))))
(def api! (memoize -api!))
