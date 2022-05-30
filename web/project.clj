(defproject thoughts "0.1.0"
  :dependencies [
    [cljs-http "0.1.46"]
    [metosin/reitit-frontend "0.5.18"]
    [metosin/reitit-spec "0.5.18"]
    [org.clojure/core.async "1.5.648"]
    [reagent "1.1.1"]]
  :profiles {:dev {:dependencies [
    [com.bhauman/figwheel-main "0.2.18"]]}}
  :aliases {
    "fig" ["trampoline" "run" "-m" "figwheel.main"]
    "fig-dev" ["trampoline" "run" "-m" "figwheel.main"
               "--build" "dev" "--repl"]})
