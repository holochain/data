(ns github.core
 (:require
  org.httpkit.client
  taoensso.timbre
  cheshire.core
  api.core
  github.data))

(defn endpoint->url [endpoint] (api.core/endpoint->url github.data/base-url endpoint))

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

(defn with-url
 [params url]
 (assoc params :url url))

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

(defn api!
 ([token endpoint] (api! token endpoint {}))
 ([token endpoint options & params]
  (taoensso.timbre/debug "Requesting Github endpoint" endpoint)
  (let [request (org.httpkit.client/request (-> (apply hash-map params)
                                                (with-url (endpoint->url endpoint))
                                                (with-options options)
                                                with-api-version-headers
                                                (with-auth-headers token)))
        response @request]
   (if (response-ok? response)
    (response->data response)
    (do
     (taoensso.timbre/error (scrub-response response))
     (throw (Exception. "Bad response from Github")))))))
