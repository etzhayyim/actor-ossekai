(ns ossekai.murakumo
  "Pure cljc actor boundary generated from manifest migration scaffold."
  (:require [clojure.string :as str]))

(def actor-did
  "did:web:ossekai.etzhayyim.com")

(def common-gates
  [:council-charter-attestation
   :no-platform-held-key-baseline
   :no-probing-baseline
   :murakumo-only-inference-baseline
   :did-primary-baseline
   :append-only-gate-baseline
   :kotoba-only-substrate-baseline])

(defn collection
  [name]
  (str "com.etzhayyim.ossekai." name))

(def cell-specs {
  :arbitrage_observer {:legacy-cell "arbitrage-observer"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "arbitrage_observer")]
     :required-gates common-gates
     :trigger "manifest cell arbitrage_observer"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :intel_analyzer {:legacy-cell "intel-analyzer"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "intel_analyzer")]
     :required-gates common-gates
     :trigger "manifest cell intel_analyzer"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :aggregate_publisher {:legacy-cell "aggregate-publisher"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "aggregate_publisher")]
     :required-gates common-gates
     :trigger "manifest cell aggregate_publisher"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :member_digest {:legacy-cell "member-digest"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "member_digest")]
     :required-gates common-gates
     :trigger "manifest cell member_digest"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :mention_dispatcher {:legacy-cell "mention-dispatcher"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "mention_dispatcher")]
     :required-gates common-gates
     :trigger "manifest cell mention_dispatcher"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :consent_registry {:legacy-cell "consent-registry"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "consent_registry")]
     :required-gates common-gates
     :trigger "manifest cell consent_registry"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :kaizen_observer {:legacy-cell "kaizen-observer"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "kaizen_observer")]
     :required-gates common-gates
     :trigger "manifest cell kaizen_observer"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :emergency_advisory {:legacy-cell "emergency-advisory"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "emergency_advisory")]
     :required-gates common-gates
     :trigger "manifest cell emergency_advisory"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
})

(defn safe-rkey
  [s]
  (let [clean (-> (str s)
                  (str/replace #"^did:web:" "")
                  (str/replace #"[^A-Za-z0-9._~-]" "-"))]
    (if (str/blank? clean) "unknown" clean)))

(defn gate-value
  [attestations gate]
  (or (get attestations gate)
      (get attestations (name gate))
      (when (set? attestations) (attestations gate))
      (when (set? attestations) (attestations (name gate)))))

(defn missing-gates
  [spec attestations]
  (->> (:required-gates spec)
       (remove #(boolean (gate-value attestations %)))
       vec))

(defn put-record-effect
  [collection rkey record]
  {:op :mst/put-record
   :actor actor-did
   :collection collection
   :rkey rkey
   :record record})

(defn records-for
  [spec {:keys [records record computed-at request-id]
         :as input}]
  (let [input-records (cond
                        (map? records) records
                        (some? record) {0 record}
                        :else {})
        base {:actorDid actor-did
              :computedAt computed-at
              :legacyCell (:legacy-cell spec)
              :phase (:phase spec)
              :requestId request-id
              :actorBoundary "cljc-migration-scaffold"
              :scaffold true
              :constitutionalStatus "attested-plan"}]
    (map-indexed
     (fn [idx coll]
       (let [record* (merge {:$type coll}
                            base
                            (or (get input-records coll)
                                (get input-records idx)
                                {}))
             rkey (safe-rkey (or (:rkey record*)
                                 (get record* "rkey")
                                 (:tid record*)
                                 request-id
                                 (str (:legacy-cell spec) "-" idx)))]
         {:collection coll
          :record record*
          :rkey rkey}))
     (:collections spec))))

(defn cell-plan
  [cell-key {:keys [attestations] :as input}]
  (let [spec (get cell-specs cell-key)]
    (when-not spec
      (throw (ex-info "unknown cell" {:cell cell-key})))
    (let [missing (missing-gates spec attestations)]
      (merge
       {:cell cell-key
        :legacy-cell (:legacy-cell spec)
        :actor actor-did
        :phase (:phase spec)
        :murakumo-node (:murakumo-node spec)
        :trigger (:trigger spec)
        :ceiling (:ceiling spec)
        :required-gates (:required-gates spec)
        :missing-gates missing}
       (if (seq missing)
         {:status :blocked
          :effects []}
         (let [planned-records (records-for spec input)]
           {:status :ready
            :records (vec planned-records)
            :effects (mapv (fn [{:keys [collection record rkey]}]
                             (put-record-effect collection rkey record))
                           planned-records)}))))))

(defn all-cell-plans
  [input]
  (into {}
        (map (fn [cell-key] [cell-key (cell-plan cell-key input)]))
        (keys cell-specs)))
