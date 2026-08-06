package com.taxin60sec.backend.entity.enums;

/**
 * A CA's self-reported capacity, set by the CA themselves (see CAProfileController's
 * PATCH /me/availability). Purely advisory to the admin case-assignment screen — it does
 * NOT block BusinessService.assign() from assigning a case to an UNAVAILABLE CA, since an
 * admin may still need to override during a backlog. It exists so the "assign to CA"
 * dropdown can show capacity at a glance next to each CA's current caseload, especially
 * during seasonal filing spikes.
 */
public enum CAAvailability {
    AVAILABLE,
    LIMITED,
    UNAVAILABLE
}