# `com.etzhayyim.ossekai.*` — Lexicon Namespace

**ADR**: ADR-2605264000 (R0 scaffold; ossekai information-arbitrage elimination + Wellbecoming nudge actor)
**Form**: 任意団体 internal artificial-organism information-arbitrage substrate
**First-touch channel**: AT Protocol — `app.bsky.feed.post` + custom feed generator + `@mention` (NO email / SMTP at R0-R2)

## Records (9)

| Lexicon | Purpose |
|---|---|
| `arbitrageGapReport` | Per-detection record of an information-asymmetry pocket; G3 STRUCTURAL passive-only + G1 Charter Rider scan |
| `wellbecomingAdvisory` | Curated advisory; G10 framing-audit pass + G11 anti-individualism audience-share distribution + UPL/medical/financial boundary citations |
| `feedPostAttestation` | Per AT Proto post emission audit-trail; G9 signed sender DID + G15 mute/block check pass |
| `memberDigestSubscription` | Adherent SBT member opt-in subscription; per-category granularity; cadence default weekly |
| `memberDigestRecord` | Per-delivery audit-trail; G8 STRUCTURAL encryptedPayloadCid REQUIRED |
| `mentionDispatchAttestation` | Council Lv6+ ≥3 (≥4 if >50 handles) attestation authorizing non-member @mention campaign |
| `externalMentionConsent` | Non-member explicit prior consent; per-category granularity; ≤365-day expiry; revocable |
| `unsubscribeRecord` | Unified unsubscribe; ingests AT Proto block/mute; G15 STRUCTURAL effective immediately at projection layer |
| `silenOssekaiReview` | Quarterly Council audit; G14 STRUCTURAL: reEngagementAfterOptOutCount=0 const + commercialIntelCrmSoftwarePenetrationPct=0 const + framingAuditWellbecomingPreservationCompliant true const + anonAggregateSharePctIntegerHundredths ≥5000 |

## Structural enforcement summary

The structural-enforcement strategy follows the chigiri / toritate /
iyashi / mizuho / kazaori pattern — gates are enforced at the schema
layer via `const` fields and `minLength` constraints wherever possible,
so that a malformed dispatch is rejected before it can reach the
projection layer.

| Gate | Lexicon-layer structural enforcement |
|---|---|
| G1 Charter Rider scan | `charterRiderScanPass: const true` in arbitrageGapReport / wellbecomingAdvisory / feedPostAttestation / mentionDispatchAttestation |
| G3 PASSIVE-ONLY | `passiveOnlyAttested: const true` + `sourceCids minLength 1` requiring pre-published archive CIDs |
| G4 Aggregate-first | silenOssekaiReview `anonAggregateSharePctIntegerHundredths minimum 5000` (≥50% aggregate-public-feed share) |
| G5 NO commercial intel/CRM | silenOssekaiReview `commercialIntelCrmSoftwarePenetrationPct: const 0` |
| G7 Rate limit | mentionDispatchAttestation `rateLimitWindowAttested: const true` |
| G8 Encrypted envelope | memberDigestRecord `encryptedPayloadCid: required` |
| G9 Signed sender DID | feedPostAttestation `senderDidConst: const "did:web:ossekai.etzhayyim.com"` |
| G10 Wellbecoming framing | wellbecomingAdvisory / feedPostAttestation / mentionDispatchAttestation `framingAuditPass: const true` |
| G11 Anti-individualism | wellbecomingAdvisory `audienceShareDistribution: required` |
| G13 Council attestation | mentionDispatchAttestation `attestingCouncilDids minLength 3`; consumer enforces ≥4 if campaignSize>50 |
| G14 Quarterly audit | silenOssekaiReview `councilAttestations minLength 3` |
| G15 mute/block honored | unsubscribeRecord `effectiveImmediatelyAttested: const true` + silenOssekaiReview `reEngagementAfterOptOutCount: const 0` |

## Cross-actor citations

- **chigiri (ADR-2605262700)** — UPL boundary: legal-themed advisories cite `com.etzhayyim.chigiri.ipLicenseClaim` for licensed-counsel routing
- **iyashi (ADR-2605263000)** — medical-advice boundary
- **mitate** — diagnostic boundary
- **yakushi (ADR-2605250500)** — pharmaceutical boundary
- **toritate (ADR-2605262900)** — financial-transparency intel SOURCE (toritate publishes; ossekai reads)
- **kazaori (ADR-2605263200)** — emergency-advisory cross-actor (`memberDigestRecord.deliveryCadence: ad-hoc-emergency` triggered by kazaori.emergencyDeclarationAttestation)

## AT Protocol primitives used (existing membrane)

- `app.bsky.feed.post` — primary publication channel (membrane per ADR-2605231902 preserved unchanged)
- `app.bsky.feed.generator` — custom feed `feed.ossekai.wellbecoming`
- `app.bsky.graph.block` — ingested by unsubscribeRecord (G15)
- `app.bsky.graph.mute` — ingested by unsubscribeRecord (G15)
- `did:web:ossekai.etzhayyim.com` — signed sender DID (G9)

## Related files

- `/90-docs/adr/2605264000-ossekai-information-arbitrage-tier-b-actor-r0.md` — Master ADR
- `/` — Actor scaffold
- `/orgs/etzhayyim/com-etzhayyim-chigiri/wire/` — UPL boundary cross-actor
- `/00-contracts/lexicons/com/etzhayyim/iyashi/` — medical-advice boundary cross-actor
- `/CHARTER-RIDER.md` §2(c) + §2(e) — G3 + G5 sources
