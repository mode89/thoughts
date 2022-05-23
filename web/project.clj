(defproject thoughts "0.1.0"
  :dependencies [
    [reagent "1.1.1"]]
  :profiles {:dev {:dependencies [
    [com.bhauman/figwheel-main "0.2.18"]]}}
  :aliases {
    "fig" ["trampoline" "run" "-m" "figwheel.main"]
    "fig-dev" ["trampoline" "run" "-m" "figwheel.main"
               "--build" "dev" "--repl"]})
