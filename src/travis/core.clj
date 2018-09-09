(ns travis.core
 (:require
  travis.data
  api.core
  org.httpkit.client))

(def endpoint->url (partial api.core/endpoint->url travis.data/base-url))

(def iso8601-> (partial time.core/format-> :date-time-no-ms))

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

(defn -api!
 ([token endpoint] (-api! token endpoint {}))
 ([token endpoint options & params]
  (taoensso.timbre/debug "Requesting Travis endpoint" endpoint)
  (let [params' (-> (apply hash-map params)
                 (api.core/with-url (endpoint->url endpoint))
                 with-api-version-headers
                 (with-auth-headers token))
        request (org.httpkit.client/request params')]
   (-> request
    deref
    throw-bad-response!
    response->data))))
(def api! (memoize -api!))
