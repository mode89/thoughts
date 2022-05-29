(ns ^:figwheel-hooks thoughts.core
  (:require [cljs-http.client :as http]
            [clojure.core.async :refer [chan close! go go-loop put! <!]]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(def BACKEND-URL "http://192.168.0.101:5000")

(def credentials (chan))

(defn signin-form []
  (let [row (fn [x] [:div {:class "row"}
                      [:div {:class "col"}
                        x]])
        state (r/atom {})
        on-change #(swap! state assoc
                          (-> % .-target .-id keyword)
                          (-> % .-target .-value))]
    [:form {:id "signin"
            :on-submit
              (fn [e] (do
                (.preventDefault e)
                (put! credentials @state)))}
      [:div {:id "center" :class "container"}
        (row [:input {:id "email"
                      :type "email"
                      :class "form-control my-1"
                      :placeholder "E-mail"
                      :on-change on-change}])
        (row [:input {:id "password"
                      :type "password"
                      :class "form-control my-1"
                      :placeholder "Password"
                      :on-change on-change}])
        (row [:button {:type "submit"
                       :style {:width "100%"}
                       :class "btn btn-primary my-1"}
                       "Sign in"])]]))

(defn signup-form []
  (let [row (fn [x] [:div {:class "row"}
                      [:div {:class "col"}
                        x]])]
    [:form {:id "signin"}
      [:div {:id "center" :class "container"}
        (row [:input {:id "email"
                      :type "email"
                      :class "form-control my-1"
                      :placeholder "E-mail"}])
        (row [:input {:id "password"
                      :type "password"
                      :class "form-control my-1"
                      :placeholder "Password"}])
        (row [:input {:id "repeat-password"
                      :type "password"
                      :class "form-control my-1"
                      :placeholder "Repeat password"}])
        (row [:button {:type "submit"
                       :style {:width "100%"}
                       :class "btn btn-primary my-1"}
                       "Sign up"])]]))

(defn new-thought-form []
  [:form {:id "new-thought"}
    [:textarea {:id "text"
                :class "form-control"
                :placeholder "Something worth remembering"
                :auto-focus true}]])

(defn random-thought [text]
  [:div {:id "random-thought"} text])

(defonce state (r/atom {:content (signin-form)}))

(defn content []
  (:content @state))

(rdom/render [content] (js/document.getElementById "app"))

(defn authenticate [credentials]
  (http/post (str BACKEND-URL "/auth") {:form-params credentials}))

(def go-authentication
  (go-loop []
    (let [cred (<! credentials)
          response (<! (authenticate (select-keys cred [:email :password])))]
      (if (:success response)
        (swap! state assoc :token (get-in response [:body :token]))
        (js/alert "Failed to authenticate."))
      (recur))))

(defn ^:before-load before-load []
  (do
    (close! go-authentication)))
