package it.unibo.alchemist.model.cognitive.impact.cognitive

import it.unibo.alchemist.model.cognitive.CognitiveModel

/**
 * The perception of the situation danger.
 * The name belief derives from the [IMPACT model](https://doi.org/10.1007/978-3-319-70647-4_11).
 *
 * @param zoneDangerousness
 *          the influence of the position of the agent owning this
 *          compared to the real position of the source of danger.
 * @param fear
 *          the level of fear of the agent owning this.
 * @param influencialPeople
 *          the list of cognitive agents with an influence on the agent owning this.
 */
class BeliefDanger(
    private val zoneDangerousness: () -> Double,
    private val fear: () -> Double,
    private val influencialPeople: () -> List<CognitiveModel>,
) : MentalCognitiveCharacteristic() {
    override fun combinationFunction(): Double = maxOf(
        sensingOmega * zoneDangerousness(),
        persistingOmega * level(),
        (affectiveBiasingOmega * fear() + influencialPeople().aggregateDangerBeliefs()) /
            (affectiveBiasingOmega + 1),
    )

    private fun List<CognitiveModel>.aggregateDangerBeliefs() =
        if (isEmpty()) 0.0 else sumOf { it.dangerBelief() } / size
}
