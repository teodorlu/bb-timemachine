# bb-timemachine

Run code back in Git-time.

## Rationale

Ever forgot to commit a file before pushing?
I have.
It sucks!

We can avoid that by testing the code you would actually push.
`bb-timemachine` lets you test the *latest commit*, rather than your current directory.
It can also run arbitrary functions or arbitrary shell commands at arbitrary points in Git-time.

## `bb test-latest-commit`

Write a [Babashka task] to test your latest commit like this:

```clojure
;; bb.edn
{:deps {io.github.teodorlu/bb-timemachine {:git/tag "v1.0.1" :git/sha "e684c"}}

 :tasks
 {test
  (shell "YOUR TEST COMMAND")

  timemachine
  {:requires ([teodorlu.bb-timemachine])
   :task (apply teodorlu.bb-timemachine/timemachine *command-line-args*)}

  test-latest-commit
  (shell "bb timemachine HEAD -- bb test")}}
```

[Babashka task]: https://book.babashka.org/#tasks

Substitute `(shell "YOUR TEST COMMAND")` for code that runs your test suite.
Then run it!

```
bb test-latest-commit
```

(which is just shorthand for `bb timemachine HEAD -- bb test`).

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

## Gratitude

Thank you to Michiel Borkent for making great tools like Babashka, babashka/fs and babashka process.
And for helpfully guiding me in their proper usage.

Thank you to Mathias, Sigmund, Ole Marius, Christian and Magnar for being great colleagues.
Without you lot being willing to try new ways of working, bb-timemachine would not exist!
