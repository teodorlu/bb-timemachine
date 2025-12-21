(ns teodorlu.bb-timemachine-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is testing]]
            [teodorlu.bb-timemachine :as timemachine]))

(set! *warn-on-reflection* true)

(def beginning-of-time "2bce149ff003d8c372db63717459ca8c3d241508")

(deftest worktree-add-remove
  (let [tempdir (fs/create-temp-dir)
        worktree-dir (str (fs/file tempdir beginning-of-time))
        worktree-list-dir #(when (fs/exists? worktree-dir)
                             (fs/list-dir worktree-dir))]
    (testing "At first, there are no files in the worktree folder"
      (is (empty? (worktree-list-dir))))

    (testing "After worktree-add, we can find our README in the worktree folder"
      (timemachine/worktree-add "." worktree-dir beginning-of-time)
      (try
        (is (contains? (set (map fs/file-name (worktree-list-dir)))
                       "deps.edn"))
        (finally
          (timemachine/worktree-remove "." worktree-dir))))

    (testing "After worktree-remove, the folder is empty."
      (is (empty? (worktree-list-dir))))))

(deftest do-at
  (is (= (timemachine/do-at beginning-of-time (constantly ::result))
         ::result))
  (is (contains? (timemachine/do-at beginning-of-time
                   (fn [dir]
                     (->> (fs/list-dir dir)
                          (map fs/file-name)
                          set)))
                 "deps.edn")))
