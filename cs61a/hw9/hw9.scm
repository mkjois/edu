;;; Name: Manohar Jois
;;; Login: cs61a-3u
;;; TA: Allen Nguyen
;;; Section: 203

(define (assert-equal v1 v2)
  (if (equal? v1 v2)
    (print 'ok)
    (print (list v2 'does 'not 'equal v1))))

;;; Q1.

(define (test-q1)
  (define counts (list 1 2 3 4))
  (assert-equal 2 (cadr counts))
  (assert-equal 3 (caddr counts)))

(define (cddr s)
  (cdr (cdr s)))

(define (cadr s)
  (car (cdr s)))

(define (caddr s)
  (car (cddr s)))

;;; Q2.

(define (test-q2)
  (assert-equal -1 (sign -42))
  (assert-equal 0 (sign 0))
  (assert-equal 1 (sign 42)))

(define (sign x)
  (cond 
	((< x 0) -1)
	((= x 0) 0)
	(else 1)))

; Derive returns the derivative of exp with respect to var.
(define (derive exp var)
  (cond ((number? exp) 0)
        ((variable? exp) (if (same-variable? exp var) 1 0))
        ((sum? exp) (derive-sum exp var))
        ((product? exp) (derive-product exp var))
        ((exponentiation? exp) (derive-exponentiation exp var))
        (else 'Error)))


; Variables are represented as symbols
(define (variable? x) (symbol? x))
(define (same-variable? v1 v2)
  (and (variable? v1) (variable? v2) (eq? v1 v2)))

; Numbers are compared with =
(define (=number? exp num)
  (and (number? exp) (= exp num)))

; Sums are represented as lists that start with +.
(define (make-sum a1 a2)
  (cond ((=number? a1 0) a2)
        ((=number? a2 0) a1)
        ((and (number? a1) (number? a2)) (+ a1 a2))
        (else (list '+ a1 a2))))
(define (sum? x)
  (and (pair? x) (eq? (car x) '+)))
(define (addend s) (cadr s))
(define (augend s) (caddr s))

; Products are represented as lists that start with *.
(define (make-product m1 m2)
(cond ((or (=number? m1 0) (=number? m2 0)) 0)
      ((=number? m1 1) m2)
      ((=number? m2 1) m1)
      ((and (number? m1) (number? m2)) (* m1 m2))
      (else (list '* m1 m2))))
(define (product? x)
  (and (pair? x) (eq? (car x) '*)))
(define (multiplier p) (cadr p))
(define (multiplicand p) (caddr p))

(define (test-sum)
  (assert-equal '(+ a x) (make-sum 'a 'x))
  (assert-equal '(+ a (+ x 1)) (make-sum 'a (make-sum 'x 1)))
  (assert-equal 'x (make-sum 'x 0))
  (assert-equal 'x (make-sum 0 'x))
  (assert-equal 4 (make-sum 1 3)))

(define (test-product)
  (assert-equal '(* a x) (make-product 'a 'x))
  (assert-equal 0 (make-product 'x 0))
  (assert-equal 'x (make-product 1 'x))
  (assert-equal 6 (make-product 2 3)))

;;; Q3.

(define (test-q3)
  (assert-equal 1 (derive '(+ x 3) 'x)))

(define (derive-sum exp var)
  (make-sum 
	(derive (addend exp) var)
	(derive (augend exp) var)))

;;; Q4.

(define (test-q4)
  (assert-equal 'y (derive '(* x y) 'x))
  (assert-equal '(+ (* x y) (* y (+ x 3)))
                (derive '(* (* x y) (+ x 3)) 'x)))

(define (derive-product exp var)
  (make-sum
	(make-product
		(multiplier exp)
		(derive (multiplicand exp) var))
	(make-product
		(derive (multiplier exp) var)
		(multiplicand exp))))

;;; Q5.

(define (test-q5)
  (assert-equal 1024 (pow 2 10))
  (assert-equal 243 (pow 3 5)))

(define (square x) (* x x))

(define (pow b n)
  (cond
	((= n 0) 1)
	((< n 0) (/ 1 (pow b (- n))))
	((even? n) (pow (square b) (/ n 2)))
	(else (* b (pow b (- n 1))))))

;;; Q6.

(define (test-q6)
  (let ((x^2 (make-exponentiation 'x 2))
        (x^3 (make-exponentiation 'x 3)))
    (assert-equal 'x (make-exponentiation 'x 1))
    (assert-equal 1 (make-exponentiation 'x 0))
    (assert-equal 16 (make-exponentiation 2 4))
    (assert-equal (make-product 3 x^2) (derive x^3 'x))
    (assert-equal '(* 2 x) (derive x^2 'x))))

(define (make-exponentiation base exponent)
  (cond
	((=number? exponent 0) 1)
	((=number? exponent 1) base)
	((and (number? base) (integer? exponent)) (pow base exponent))
	(else (list 'pow base exponent))))

(define (base exponentiation)
  (cadr exponentiation))

(define (exponent exponentiation)
  (caddr exponentiation))

(define (exponentiation? exp)
  (and (pair? exp) (eq? (car exp) 'pow)))

(define (derive-exponentiation exp var)
  (make-product
	(make-product
		(exponent exp)
		(make-exponentiation
			(base exp)
			(- (exponent exp) 1)))
	(derive (base exp) var)))
	
(test-q1)
(test-q2)
(test-q3)
(test-q4)
(test-q5)
(test-q6)
