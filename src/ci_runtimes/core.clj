(ns ci-runtimes.core
  (:gen-class)
  (:require [clojure.pprint :as pprint]
            [clojure.instant :as instant]
            [ci-runtimes.github-api :as github-api]))

(defn line [n]
  (apply str (repeat n "#")))

(def max-line-length 15)

(defn line-for-duration [dur min-duration max-duration]
  (if (not= min-duration max-duration)
    (line (inc (* (- dur min-duration) (/ (dec max-line-length) (- max-duration min-duration)))))
    (line 1)))

(defn average [items]
  (if (pos? (count items))
    (/ (reduce + items) (count items))
    nil))

(defn diff-in-seconds [end start]
  (/ (- (.getTime end) (.getTime start)) 1000))

(defn diff-of-strings-in-seconds [end-str start-str]
  (when (and end-str start-str)
    (diff-in-seconds
     (instant/read-instant-date end-str)
     (instant/read-instant-date start-str))))

(defn date-to-iso-string [date]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") date))

(defn day-item-from-job [job] {:duration (diff-of-strings-in-seconds (:completed_at job) (:started_at job))
                               :date (date-to-iso-string (instant/read-instant-date (:started_at job)))})

(defn filter-success [job]
  (zero? (compare (:conclusion job) "success")))

(defn start-gather [owner repo workflow-name]
  (println "Get workflow" (str "\"" workflow-name "\"") "of" (str owner "/" repo) "...")
  (let [workflow_id (github-api/get-workflow-id-by-name owner repo workflow-name)]
    (println "Got workflow id:" workflow_id)
    (println "Get jobs of the workflow...")
    (let [job_urls (github-api/get-workflow-run-ids owner repo workflow_id)
          job (->> job_urls
                   (map github-api/get-jobs)
                   ;; TODO: specify which job to take from workflow run, now takes the first
                   (map #(first %)))
          day-items (->> job
                         (filter (complement nil?))  ;; CI job not started
                         (filter filter-success)
                         (map #(day-item-from-job %)))
          durations (->> day-items
                         (map #(:duration %))
                         (filter number?))
          average-duration (->> durations
                                (average)
                                (float))
          grouped-day-items (->> day-items
                                 (group-by :date) ;; -> {"2021-05-21" [{:date "2021-05-21" :duration 4}]}
                                 (map #(hash-map :date (key %)
                                                 :duration (->>
                                                            (val %)
                                                            (map :duration)
                                                            (average)
                                                            (float)
                                                            (Math/round))
                                                 :runs (->>
                                                        (val %)
                                                        (count))
                                                ;;  :duration (Math/round (float (average (map :duration (val %)))))
                                                 )))
          max-duration (->> grouped-day-items
                            (map #(:duration %))
                            (apply max))
          min-duration (->> grouped-day-items
                            (map #(:duration %))
                            (apply min))
          grouped-line-added (->> grouped-day-items
                                  (map #(assoc % :line (line-for-duration (:duration %) min-duration max-duration))))]
      ;; (println "Workflow run durations are" grouped-day-items)
      ;; (println "Workflow run durations are" grouped-line-added)
      (pprint/print-table (sort-by (fn [item] (:date item)) grouped-line-added))
      ;; TODO: humanize the durations
      (println)
      (println "Average duration of the workflow runs is" average-duration "seconds"))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (== (count args) 3)
    (let [[owner repo workflow-name] args]
      (start-gather owner repo workflow-name))
    (do
      (println "Give params: owner, repo and workflow name")
      (println "Example: lein run colinhacks zod \"Fix, Format, Regenerate\""))))
