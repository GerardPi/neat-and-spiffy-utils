#!/usr/bin/env clj
(ns configure-displays
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as io]))

;;
;; This script expects the Clojure binary in the PATH.
;; It uses the Clojure shell library (I expect you'll need Clojure 1.8 or higher) to execute external processes.
;;
;; In this script on Linux (runs in Debian 10), xrandr is used to configure the displays.
;;
;; Using a script like this, you can automate your display configuration, 
;; instead of dealing with the GUI that pops up, and requires you do the same thing each time.
;;


;; The laptop's built-in montor has this label in xrandr
(def display-laptop "LVDS-1")
;; The laptop's external D-SUB VGA connector has this label in xrandr.
(def display-ext    "VGA-1")

(def display-params { 
  "laptop" ["--output" display-laptop "--mode" "1600x900"]
  "compaq" ["--output" display-ext    "--mode" "1280x1024"]
  "iiyama" ["--output" display-ext    "--mode" "1920x1200"]
  })

(def choices {
  "1" "compaq"
  "2" "iiyama"})

(defn notify-user-when-problem [shell-command shell-result]
  "Display a message with stdout, stderr and exit code of the shell process.
   When exit code == 0, nothing is displayed."
  (when (not= (:exit shell-result) 0)
    (println "There was a problem when executing '" (apply str shell-command) "':")
    (println "Sub process exit code: " (:exit shell-result))
    (println "Error message: " (:err shell-result))
    (println "Message: " (:out shell-result))))

(defn config-display-with-params [display-params]
  "Call xrandr and configure a display with some display parameters."
  (let [shell-command (cons "xrandr" display-params)
        shell-result (apply shell/sh shell-command)]
    (notify-user-when-problem shell-command shell-result)
    shell-result))


(defn display-list-monitors []
  "Display the list of monitors as reported by xrandr."
  (let [shell-command ["xrandr" "--listmonitors"]
        shell-result (apply shell/sh shell-command)]
    (notify-user-when-problem shell-command shell-result)
    (when (= (:exit shell-result) 0)
      (println "Current monitor information: ")
      (println (:out shell-result)))
    shell-result))

(defn get-display-params [display-name] 
  "Fetching a value from a map or, when not available, display a complaint."
  (let [display-params (get display-params display-name)]
    (if display-params
      display-params
      (println "Can not find display params for '" display-name "'"))))

(defn config-display [display-name]
  (let [display-params (get-display-params display-name)]
    (println "configuring display" display-name "with params" display-params)
    (config-display-with-params display-params)))

(defn config-position [display-top display-bottom]
  (println "configuring display" display-top "above" display-bottom)
  (let [shell-command ["xrandr" "--output" display-top "--above" display-bottom]
        shell-result (apply shell/sh shell-command)]
    (notify-user-when-problem shell-command shell-result)
    shell-result))

(defn display-menu []
  (println "What type of display do you have as 2nd display?")
  (doseq [[choice name] choices]
    (println "->" choice name)))

(defn main [] 
  (display-list-monitors)

  (display-menu)
  (let [choice (read-line)
        chosen-display-name (get choices choice)]

    (println (str "chosen display name: '%s'" chosen-display-name))

    (config-display "laptop")
    (config-display chosen-display-name)
    (config-position display-ext display-laptop)))

(main)
(System/exit 0)
