(ns ossekai.murakumo-test
  (:require [clojure.test :refer [deftest is testing]]
            [ossekai.murakumo :as ossekai]))

(def full-attestations
  (into {}
        (map (fn [gate] [gate (str "attested-" (name gate))]))
        (distinct (mapcat :required-gates (vals ossekai/cell-specs)))))

(deftest maps-all-legacy-ossekai-cells
  (is (= #{"ossekai_aggregate_publisher"
           "ossekai_arbitrage_observer"
           "ossekai_consent_registry"
           "ossekai_emergency_advisory"
           "ossekai_intel_analyzer"
           "ossekai_kaizen_observer"
           "ossekai_member_digest"
           "ossekai_mention_dispatcher"}
         (set (map :legacy-cell (vals ossekai/cell-specs))))))

(deftest r0-gates-block-effects
  (let [plan (ossekai/cell-plan :arbitrage-observer
                                {:topic "cooling-off"
                                 :computed-at "2026-06-29T00:00:00Z"})]
    (is (= :blocked (:status plan)))
    (is (= [:council-charter-attestation
            :silen-ossekai-baseline-review
            :r1-activation-adr
            :atproto-first-touch-baseline
            :murakumo-only-inference-baseline
            :signed-sender-did-baseline
            :passive-only-source-baseline
            :no-active-probe-baseline
            :charter-rider-clean-input-output-baseline
            :e7m-dataset-tier-a-foundations
            :legal-corpus-source-baseline]
           (:missing-gates plan)))
    (is (empty? (:effects plan)))))

(deftest attested-mention-plan-emits-atproto-and-audit-effects
  (let [plan (ossekai/cell-plan :mention-dispatcher
                                {:attestations full-attestations
                                 :topic "cooling-off"
                                 :campaign-id "campaign-001"
                                 :handle "example.com"
                                 :computed-at "2026-06-29T00:00:00Z"
                                 :record {:tid "mention-001"
                                          :state "draft"
                                          :muteBlockCheckedBeforeComposition true}})
        collections (map :collection (:effects plan))]
    (is (= :ready (:status plan)))
    (is (= [:mst/put-record :mst/put-record :mst/put-record]
           (map :op (:effects plan))))
    (is (= ["app.bsky.feed.post"
            "com.etzhayyim.ossekai.mentionDispatchAttestation"
            "com.etzhayyim.ossekai.feedPostAttestation"]
           collections))
    (is (every? #(= ossekai/actor-did (:actor %)) (:effects plan)))
    (is (= "mention-001" (:rkey (first (:effects plan)))))
    (is (= true (get-in (first (:effects plan)) [:record :muteBlockCheckedBeforeComposition])))))

(deftest special-gates-remain-cell-specific
  (testing "member digest keeps encrypted envelope gate"
    (let [attestations (dissoc full-attestations :encrypted-envelope-baseline)
          plan (ossekai/cell-plan :member-digest {:attestations attestations})]
      (is (= [:encrypted-envelope-baseline] (:missing-gates plan)))
      (is (empty? (:effects plan)))))
  (testing "aggregate publisher keeps aggregate-first gate"
    (let [attestations (dissoc full-attestations :aggregate-first-default-baseline)
          plan (ossekai/cell-plan :aggregate-publisher {:attestations attestations})]
      (is (= [:aggregate-first-default-baseline] (:missing-gates plan)))
      (is (empty? (:effects plan)))))
  (testing "emergency advisory keeps kazaori declaration gate"
    (let [attestations (dissoc full-attestations :kazaori-emergency-declaration-attestation)
          plan (ossekai/cell-plan :emergency-advisory {:attestations attestations})]
      (is (= [:kazaori-emergency-declaration-attestation] (:missing-gates plan)))
      (is (empty? (:effects plan))))))

(deftest all-cell-plans-ready-when-attested
  (let [plans (ossekai/all-cell-plans {:attestations full-attestations
                                       :topic "cooling-off"
                                       :campaign-id "campaign-001"
                                       :member-did "did:example:member"
                                       :handle "example.com"
                                       :computed-at "2026-06-29T00:00:00Z"})]
    (is (= (set (keys ossekai/cell-specs)) (set (keys plans))))
    (is (every? #(= :ready (:status %)) (vals plans)))
    (is (= 14 (count (mapcat :effects (vals plans)))))))
