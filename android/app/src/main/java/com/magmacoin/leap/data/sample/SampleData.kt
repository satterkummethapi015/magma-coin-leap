package com.magmacoin.leap.data.sample

import com.magmacoin.leap.R
import com.magmacoin.leap.domain.model.GearItem

object SampleData {

    const val ID_BOOTS = "basalt_boots"
    const val ID_GOGGLES = "ash_goggles"
    const val ID_POUCH = "ember_pouch"
    const val ID_HARNESS = "obsidian_harness"
    const val ID_CLOAK = "thermal_cloak"
    const val ID_BEACON = "summit_beacon"

    val GEAR: List<GearItem> = listOf(
        GearItem(ID_BOOTS, R.string.gear_boots_name, R.string.gear_boots_effect, 60),
        GearItem(ID_GOGGLES, R.string.gear_goggles_name, R.string.gear_goggles_effect, 120),
        GearItem(ID_POUCH, R.string.gear_pouch_name, R.string.gear_pouch_effect, 200),
        GearItem(ID_HARNESS, R.string.gear_harness_name, R.string.gear_harness_effect, 320),
        GearItem(ID_CLOAK, R.string.gear_cloak_name, R.string.gear_cloak_effect, 480),
        GearItem(ID_BEACON, R.string.gear_beacon_name, R.string.gear_beacon_effect, 700)
    )
}
