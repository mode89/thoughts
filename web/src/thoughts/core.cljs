(ns ^:figwheel-hooks thoughts.core
  (:require [cljs-http.client :as http]
            [clojure.core.async :refer [chan close! go go-loop put! <!]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reitit.coercion.spec :as rcs]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(def BACKEND-URL "http://192.168.0.101:5000")

(def credentials (chan))

(defn signin-view [route]
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

(defn signup-view [route]
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

(defn new-thought-view [route]
  [:form {:id "new-thought"}
    [:textarea {:id "text"
                :class "form-control"
                :placeholder "Something worth remembering"
                :auto-focus true}]
    [:button {:type "submit"
              :class "btn btn-primary"}
              "Save"]])

(defn random-thought-view [route]
  (let [{:keys [path query]} (:parameters route)
        {:keys [id]} path]
    [:div {:id "random-thought"}
      [:div {:id "text"} id]
      [:button {:id "new-thought-btn"
                :class "btn"
                :on-click #(rfe/push-state ::new-thought)}
        "New thought"]]))

(defonce token (r/atom nil))
(defonce route (r/atom nil))

(defn current-page []
  [(:view (:data @route)) @route])

(defn authenticate [credentials]
  (http/post (str BACKEND-URL "/auth") {:form-params credentials}))

(def go-authentication
  (go-loop []
    (let [cred (<! credentials)
          response (<! (authenticate (select-keys cred [:email :password])))]
      (if (:success response)
        (do
          (reset! token (get-in response [:body :token]))
          (rfe/replace-state ::random-thought {:id 42}))
        (js/alert "Failed to authenticate."))
      (recur))))

(defn ^:before-load before-load []
  (do
    (close! go-authentication)))

(defn init! []
  (rfe/start!
    (rf/router
      [["/"
         {:name ::root
          :view signin-view}]
       ["/random-thought/{id}"
         {:name ::random-thought
          :view random-thought-view}]
       ["/new-thought"
         {:name ::new-thought
          :view new-thought-view}]])
    #(reset! route %)
    {:use-fragment true})
  (rdom/render [current-page] (js/document.getElementById "app")))

(init!)
