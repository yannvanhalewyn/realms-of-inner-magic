(ns rim.character
  (:require [medley.core :as m]))

(def characters
  (m/index-by
   :character/class
   [{:character/class :mage
     :character/texture "/img/characters/1_IDLE_000.png"
     :character/scale [0.25 0.25]}
    {:character/class :elf
     :character/texture "/img/characters/Elf_01__IDLE_000.png"
     :character/scale [0.17 0.17]}
    {:character/class :warrior
     :character/texture "/img/characters/Warrior_01__IDLE_000.png"
     :character/scale [0.09 0.09]}]))

(defn config [class]
  (get characters class))

(defn random-class []
  (rand-nth (keys characters)))

(defn texture [class]
  (get-in characters [class :character/texture]))
