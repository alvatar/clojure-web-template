(ns the-resellers.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [the-resellers.core-test]
   [the-resellers.common-test]))

(enable-console-print!)

(doo-tests 'the-resellers.core-test
           'the-resellers.common-test)
