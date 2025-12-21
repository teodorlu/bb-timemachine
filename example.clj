(ns example
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]
            [teodorlu.bb-timemachine :as timemachine]))

(def beginning-of-time
  (-> (p/shell {:out :string} "git rev-list --all")
      :out
      str/split-lines
      last))

beginning-of-time
;; => "2bce149ff003d8c372db63717459ca8c3d241508"

(timemachine/do-at beginning-of-time (fn [dir]
                                       {:dir dir
                                        :files (map fs/file-name (fs/list-dir dir))}))
;; => {:dir
;;     "/var/folders/kk/d19_bvxd5mzgqvzzx05c6ry80000gn/T/613c303c-6912-4223-b12d-5a0da8f81b3813903811052838475498/2bce149ff003d8c372db63717459ca8c3d241508",
;;     :files ("deps.edn" "bb.edn" ".git" "src")}

(timemachine/do-at beginning-of-time (fn [dir]
                                       (->> (fs/glob dir "**/*")
                                            (map #(fs/relativize dir %))
                                            (map str))))
;; => ("src/teodorlu" "src/teodorlu/bb_timemachine.clj")
