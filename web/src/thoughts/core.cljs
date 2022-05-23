(ns thoughts.core
  (:require [reagent.dom :as rdom]))

(defn signin-form []
  (let [row (fn [x] [:div {:class "row"}
                      [:div {:class "col"}
                        x]])]
    [:div (row [:input {:id "email"
                        :type "email"
                        :class "form-control my-1"
                        :placeholder "E-mail"}])
          (row [:input {:id "password"
                        :type "password"
                        :class "form-control my-1"
                        :placeholder "Password"}])
          (row [:button {:type "submit"
                         :style {:width "100%"}
                         :class "btn btn-primary my-1"}
                         "Sign in"])]))

(rdom/render [signin-form] (js/document.getElementById "app"))
