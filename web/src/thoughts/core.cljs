(ns thoughts.core
  (:require [reagent.dom :as rdom]))

(defn hello-world []
  [:div "Hello, World!"])

(rdom/render [hello-world] (js/document.getElementById "app"))
