(ns myproject.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [myproject.core-test]
   [myproject.common-test]))

(enable-console-print!)

(doo-tests 'myproject.core-test
           'myproject.common-test)
