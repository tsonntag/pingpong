(ns pingpong.game
  (:require
    [pingpong.lib :refer [link-to]]
    [reagent.core :refer [atom]]
    ))


; unit in px
(def unit 10)

(defn scale [i]
  (str (* unit i) "px"))

(def ball-size [1 1])
(def field-size [50 50])
(def speed0 10)

(def p0 [0 10])
(def dp0 [0.3 0.3])
(def pmin [0 0])
(def pmax (mapv - field-size ball-size))
(def state0 [p0 dp0 pmin pmax])

(def state (atom state0)) 
(def speed (atom speed0))
(def interval (atom nil))

(defn next-i [i di imin imax]
  (let [i1 (+ i di)]
    (cond
      (< i1 imin) [imin (- di) imin imax]
      (> i1 imax) [imax (- di) imin imax]
      :else [i1 di imin imax])))

(defn transpose [m] (apply mapv vector m))

(defn next-state [state]
  (transpose (apply map next-i state)))

(defn step! []
  (println "STEP")
  (swap! state next-state))

(defn running? [] @interval)

(defn stop! []
  (when (running?) 
   (js/clearInterval @interval)
   (reset! interval nil)))

(defn start! []
  (stop!)
  (let [intv (/ 1000.0 @speed)]
    (reset! interval (js/setInterval step! intv))))

(add-watch speed nil #(if (running?) (start!)))

(defn toggle []
  (if (running?) 
    (stop!)
    (start!)))

(defn ball []
  (let [[bx by] ball-size
        [x y] (first @state)]
    [:div#ball
     {:style
      {:background-color "#FF0000"
       :position "absolute"
       :margin-left (scale x)
       :margin-top  (scale y)
       :width  (scale bx)
       :height (scale by)}}]))

(defn field []
  (let [[x y] field-size]
    [:div#field
     {:style {:background-color "#F0F0F0"
              :width  (scale x)
              :height (scale y)}}
     (ball)]))

(defn speed-slider []
  [:div.speed-slider
   "Speed" 
   [:input {:type "range" :min 1 :max 1000 
            :value @speed
            :on-change #(reset! speed (float (-> % .-target .-value)))}]])

(defn page []
  (let [[p0 _ pmin pmax] @state]
    [:div [:h2 "Ping Pong"]
     [:div (link-to "#/" "home")]
     [:button {:on-click toggle} (if (running?) "Stop" "Start")]
     (speed-slider)
     (field)
     [:br]
     [:br]]
    ))
