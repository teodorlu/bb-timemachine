# bb-timemachine

Run functions and evaluate shell commands at previous points in Git-time.

## Rationale

Ever forgot to commit a file before pushing

I have.
It sucks!
And I'd rather not.

So what went wrong?
Your working directory was fine.
But the commit you pushed was *not*.

`bb-timemachine` lets you test the *latest commit*, rather than your current directory with changes.

It can also run arbitrary functions or arbitrary shell commands in arbitrary points in Git-time, but so far, it's purpose has been to catch me when I forget to commit files.

## Use it as a Babashka task

```clojure
;; bb.edn
{:deps {io.github.teodorlu/bb-timemachine {:git/sha ",,,"}}

 :tasks
 {test
  (shell "YOUR TEST COMMAND")

  timemachine
  {:requires ([teodorlu.bb-timemachine])
   :task (apply teodorlu.bb-timemachine/timemachine *command-line-args*)}

  test-latest-commit
  (shell "bb timemachine HEAD -- bb test")}}

```

See the Babashka book for more information about Babashka tasks:
https://book.babashka.org/#tasks

## `teodorlu.bb-timemachine/do-at`

You can also run Clojure functions back in time.
Here's an example:

```clojure
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
```
