package it.unibo.alchemist.model.cognitive.impact.cognitive

import it.unibo.alchemist.util.math.logistic

/**
 * The intention not to evacuate.
 *
 * @param desireWalkRandomly
 *          the desire not to evacuate of the agent owning this characteristic.
 * @param desireEvacuate
 *          the desire to evacuate of the agent owning this characteristic.
 */
class IntentionWalkRandomly(private val desireWalkRandomly: () -> Double, private val desireEvacuate: () -> Double) :
    BodyCognitiveCharacteristic() {
    override fun combinationFunction() = desireEvacuate() *
        logistic(
            logisticSigma,
            logisticTau,
            inhibitingIntentionOmega * desireEvacuate(),
            amplifyingIntentionOmega * desireWalkRandomly(),
        )
}
