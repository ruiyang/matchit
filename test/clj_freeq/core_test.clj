(ns clj-freeq.core-test
  (:require [clojure.test :refer :all]
            [clj-freeq.core :refer :all]))

(defn test-expression-with-data [expression data]
  "Given an expression, create the according filter function and apply it to data."
  ((create-filter expression) data))

(def sample-data {
           :name "simple name"
           :age 25
           :gender "male"
           :single true
           :vegeterian false
           :address {
                     :city "Melbourne"
                     :street "david street"
                     :post-code 1234
                     }})

(deftest single-value-expression-access-test
  (testing "should evaluate single value access expression"
    (is
     (= (test-expression-with-data "abc" {:abc {:def 123}})
        {:def 123})))
  (testing "should evaluate single value access expression"
    (is
     (= (test-expression-with-data "address.city" sample-data)
        "Melbourne")))
  )

(deftest simple-and-expression
  (testing "should evaluate simple and expression"
    (is
     (= (test-expression-with-data "vegeterian AND single" sample-data)
        false))))

(deftest simple-function-call
  (testing "should call simple function"
    (are [expression expected-result] (= (test-expression-with-data expression sample-data) expected-result)
         "equal(name,\"simple name\")" true
         "equal(age,24)" false
         "equal(age, 24)" false
         "equal(age, 24 )" false
         "equal(age,25)" true
         "equal(age, 25 )" true)))

(deftest simple-function-call-contains
  (testing "should test string contains"
    (are [expression expected-result] (= (test-expression-with-data expression sample-data) expected-result)
         "contains(name, \"sim\")" true
         "contains(name, \"simple \")" true
         "contains(name, \"simple name\")" true
         "contains(name, \"name\")" true
         "contains(name, \"name2\")" false)))

(deftest simple-or-expression
  (testing "should test or condition"
    (are [expression expected-result] (= (test-expression-with-data expression sample-data) expected-result)
         "equal(age, 25) OR equal(gender, \"male\")" true
         "equal(age, 24) OR equal(gender, \"female\")" false
         "equal(age, 24) OR equal(gender, \"male\")" true
         "equal(age, 24) OR equal(gender, \"male\")" true
         )))

(deftest simple-group-expression
  (testing "should test condition grouping"
    (are [expression expected-result] (= (test-expression-with-data expression sample-data) expected-result)
         "equal(gender, \"female\") OR (equal(age, 24) OR equal(name, \"simple name\"))" true
         "equal(gender, \"male\") OR (equal(age, 24) OR equal(name, \"simple name\"))" true
         "equal(gender, \"male2\") OR (equal(age, 29) OR equal(name, \"simple name2\"))" false
         )))

(deftest return-parse-tree-as-map
  (testing "should return parsed expression tree as map"
    (are [expression expected-result] (= (parse expression) expected-result)
         "equal(gender, \"female\") OR (equal(age, 24) OR equal(name, \"simple name\"))" {:OR (list {:FUNC_CALL '("equal" "gender" "'female'")} {:OR (list {:FUNC_CALL '("equal" "age" "24")} {:FUNC_CALL '("equal" "name" "'simple name'")})})}
         )))

;; (run-all-tests #"matchit.core-test")
