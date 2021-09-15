(ns ci-runtimes.core-test
  (:require [clojure.test :refer :all]
            [ci-runtimes.core :refer :all]
            [ci-runtimes.github-api :as github-api]))

(deftest a-test
  (testing "workflows-url"
    (is (= (github-api/workflows-url "some-owner" "awesome-repo")
           "https://api.github.com/repos/some-owner/awesome-repo/actions/workflows"))))
