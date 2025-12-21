(ns teodorlu.bb-timemachine
  "Run Clojure functions and shell commands back in time"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(defn rev-parse [dir git-revision]
  (-> (p/shell {:out :string :err :discard :dir dir}
               "git rev-parse" git-revision)
      :out str/trim))

(defn worktree-add [dir path commit-ish]
  (p/shell {:dir dir :out :discard :err :discard}
           "git worktree add" path commit-ish))

(defn worktree-remove [dir worktree]
  (p/shell {:dir dir :out :discard :err :discard}
           "git worktree remove" worktree))

(defn ^{:indent 1} do-at
  "Pass handle-fn a dir argument where dir is the Git repo checked out at given
  Git revision

  git-revision: eg HEAD or 91fa7c32 or a branch name
  handle-fn: function of directory where files have been checked out."
  [git-revision handle-fn]
  (let [tempdir (fs/create-temp-dir)
        repo-dir "."
        sha (rev-parse repo-dir git-revision)
        worktree-dir (str (fs/file tempdir sha))]
    (worktree-add repo-dir worktree-dir sha)
    (try
      (handle-fn worktree-dir)
      (finally
        (worktree-remove repo-dir worktree-dir)))))

(defn ^{:indent 1 :export true} timemachine
  "Entrypoint from babashka tasks.

  Encourages this pattern of usage:

    bb timemachine HEAD -- ls
    bb timemachine HEAD -- pwd"
  [revision _ & shell-command-args]
  (do-at revision
    (fn [dir] (apply p/shell {:dir dir} shell-command-args))))
