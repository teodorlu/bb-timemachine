(ns teodorlu.bb-timemachine-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is testing]]
            [teodorlu.bb-timemachine :as timemachine]))

(set! *warn-on-reflection* true)

(def initial-commit-sha "2bce149ff003d8c372db63717459ca8c3d241508")

(deftest worktree-add-remove
  (let [tempdir (fs/create-temp-dir)
        worktree-dir (str (fs/file tempdir initial-commit-sha))
        worktree-list-dir #(when (fs/exists? worktree-dir)
                             (fs/list-dir worktree-dir))]
    (testing "At first, there are no files in the worktree folder"
      (is (empty? (worktree-list-dir))))

    (testing "After worktree-add, we can find our README in the worktree folder"
      (timemachine/worktree-add "." worktree-dir initial-commit-sha)
      (try
        (is (contains? (set (map fs/file-name (worktree-list-dir)))
                       "deps.edn"))
        (finally
          (timemachine/worktree-remove "." worktree-dir))))

    (testing "After worktree-remove, the folder is empty."
      (is (empty? (worktree-list-dir))))))
