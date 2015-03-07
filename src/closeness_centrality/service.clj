(ns closeness-centrality.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [cheshire.core :refer :all]
            [closeness-centrality.core :refer :all]
            [halresource.resource :as hal]
            [ring.util.response :as ring-resp]))

(defn to-resource [url] 
  (hal/new-resource url))

(defn render-resource [resource]
  (hal/resource->representation resource :json))

(defn home [request]
  (let [resource (to-resource (route/url-for ::create-edge :absolute? true))]
    (ring-resp/response (render-resource resource))))

(defn create-edge [request]
  (let [origin (get (:params request) "origin")
        destiny (get (:params request) "destiny")
        vertices (update-current-vertices [(keyword origin) (keyword destiny)])
        customers (closeness-centrality vertices)
        sorted-customers (into (sorted-map-by (fn [key1 key2]
                                                (compare [(get customers key2) key2]
                                                         [(get customers key1) key1])))
                               customers)]
    (ring-resp/response (generate-string sorted-customers))))

(defroutes routes
  [[["/" {:get home}
     ^:interceptors [body-params/body-params]
     ["/edges" {:post create-edge}]]]])

(def service {:env :prod
              ::bootstrap/routes routes
              ::bootstrap/resource-path "/public"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})