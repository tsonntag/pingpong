(ns guess.game
  (:require
    [guess.rect :as rect]))


; game:
; menge von rects
; rect kann v haben
; "baelle" habenA v != 0
; andere rects v = 0
; Bem: 
; ein Graviationsfeld (g-Feld) ist auch ein rect
; in dem es wirkt.
;
; ** Allgemeine Überlegungen:
;
; * Wechselwirkung (ww):
; Ein Ball ist etwas auf die feste Rects 
; z.B. das Feld oder feste Hindernisse oder
; ein g-Feld einwirken können indem sie v des Ball ändern.
;
; Was ist mit Kollision von 2 Bällen b1, b2?
; ww(b1,b2) 
; Weiß b1, wie es auf b2 wirkt (also ist b1 ein Objekt mit Methode b1.ww(b2)
; Dann müsste b1.ww polymorph auf sich und den Argumenten sein.
;
; Lösung: ww ist die Funktion die das *ganze* Spiel definiert
; und polymorph in den Arugmenten ist:
; Beispiel
; ww(ball, rect) =>  ball(evtl.reflektiert), rect
; ww(balls) => balls (mit ggfs kollidierten v's)
;
; * Problem:
;   Ball kommt diagonal von oben rechts
;   wird an Wand X reflektiert und bleibt zwischen X und Y stecken.
;
;   YYYYYYY b
;          b
;   XXXXXXXXXXXXX
;   Wie kann ich erkennen, dass aus b(v!=0) => b(v=0) werden muss ?
;   Geht das mit WW von Paaren ? Wahrscheinlich nicht einfach.
;   Wenn ich ww(b,x,y) als 3-er WW betrachte: Wie erkenne ich die Lage ?
;   Wie kann ich erkennen, dass der Ball überhaupt x und y betrachten muss ?
;   
;   
; * Konzept "gueltiger" Zustand (valid state)
; Gueltige Zustände von Paaren: 
; Bsp: ein ball ist immer in demselben Feld
;
; Ein state ist valid wenn alle Paare von Elemente valid sind
; Reicht diese Art von paar-ww oder muss ich immer n-ww betrachten ?
; Wenn ich das adiabatisch betrachte (also quasi-statisch) müsste das gehen.
;
;
; * Ablauf
;
; Idee:
; Game step ist step von allen Elementen:
; Beispiel: step von ball ist Bewegung um v*dt
; Müssen Elemente, die unbeweglich sind (z.B. Feld) explizit so gekennzeichnet
; werden ? Oder reicht Spezialfall v=0 aus ?
; Nein: v=0 reicht nicht aus, denn v könnte ja beschleunigt werden.
; Also muss man beweglich Object explizit kennzeichnen.
;
; Algorithmus:
; Mache Step
; Für alle bewegl. Objekte b 
;   falls für irgendein object x das paar b,x ungültig ist,
;   revidiere step von b
;
; Hört sich nicht gut an !!
; Besser wäre wenn b beim Step schon merkt, dass er nicht weiterkommt
; und v = 0 setzt.
; Dann wären Step und ww nicht trennbar.
;
;
; * Idee:
; Next Neighbour (NN).
; Schwer: Wie behandelt man ausgedehnte Objekte ?

(def field-rect [[0 0][ 10 10]])
(def ball-rect  [[0 0][ 1  1]])
(def some-rect  [[1 1][ 4 5]])

(def state 
  {:ball
   {:rect ball-rect
    :v    [1 1]}
   :dt 1000; time steps in msecs
   :interval nil
   })

(defn pball [msg {:keys [rect v] :as ball}]
  (println msg rect "v=" v )
  ball)

(defn p [msg state]
  (pball msg (:ball state))
  state)

(def game (atom state))

(defn ball-filter [rect] 
  (fn [{:keys [ball] :as state}]
    (println "BALLFILTER ball=" ball "rect=" rect)
    (if (rect/contains-rect? rect (:rect ball))
      (do 
        (println "STUCK ball=" ball "rect=" rect)
        state))
      (assoc state :ball (rect/interact-ball ball rect))))

(defn advance-state [{:keys [ball] :as state}]
  ;(p "ADVANCE" state)
  (assoc state :ball (rect/move-ball ball)))

(def filters
  [(ball-filter field-rect)
   (ball-filter some-rect)
   ])

(defn next-state [state]
  (println "NEXT of " state)
  (let [nxt (reduce (fn [state flt] (flt state))
                    (advance-state state)
                    filters)]
    (println "NEXT   => " nxt)
    nxt))


