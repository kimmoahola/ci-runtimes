(ns ci-runtimes.github-api
  (:require [clj-http.client :as client]
            [clojure.edn :as edn]))

(def config
  (edn/read-string (slurp "config.edn")))

(def github-token (:github-token config))

;; https://github.com/settings/tokens -> no scopes needed
(defn get-url [url]
  (:body (client/get
          url
          {:accept "application/vnd.github.v3+json"
           :oauth-token github-token
           :as :json
           :query-params {:per_page 100}})))


;; https://docs.github.com/en/rest/reference/actions#list-repository-workflows
(defn workflows-url [owner repo]
  (str "https://api.github.com/repos/" owner "/" repo "/actions/workflows"))

;; https://docs.github.com/en/rest/reference/actions#list-workflow-runs
(defn workflow-run-url [owner repo run_id]
  (str (workflows-url owner repo) "/" run_id "/runs"))

(defn get-workflows [owner repo]
  (:workflows (get-url (workflows-url owner repo))))

(defn get-workflow-runs-by-id [owner repo run_id]
  (:workflow_runs (get-url (workflow-run-url owner repo run_id))))

(defn filter-by-name [name]
  (fn [item] (zero? (compare (:name item) name))))

(defn get-workflow-by-name [owner repo name]
  (first (->> (get-workflows owner repo)
              (filter (filter-by-name name)))))

(defn get-workflow-id-by-name [owner repo workflow-name]
  (:id (get-workflow-by-name owner repo workflow-name)))

;; TODO: add paging with page=1...x query param
(defn get-workflow-run-ids [owner repo workflow_id]
  (->> (get-workflow-runs-by-id owner repo workflow_id)
      ;;  (take 15)
       (map (fn [item] (:jobs_url item)))))

;; https://docs.github.com/en/rest/reference/actions#list-jobs-for-a-workflow-run
(defn get-jobs [job_url]
  (println "get-jobs" job_url)
  (:jobs (get-url job_url)))
