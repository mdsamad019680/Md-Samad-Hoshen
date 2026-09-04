package com.example.data.local

import com.example.data.model.InputReceiveEntity
import com.example.data.model.LineBalanceEntity
import com.example.data.model.ManpowerEntity
import com.example.data.model.OperationEntity
import com.example.data.model.POEntity
import com.example.data.model.ProductionOutputEntity
import com.example.data.model.StyleEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DemoDataSeeder {

    fun getInitialUsers(): List<UserEntity> {
        return listOf(
            UserEntity(1, "admin", "Alex Morrison", UserRole.ADMIN, "Plant Management"),
            UserEntity(2, "prod_officer", "Rahim Ahmed", UserRole.PRODUCTION_OFFICER, "Sewing Floor"),
            UserEntity(3, "line_chief", "Tariqul Islam", UserRole.LINE_CHIEF, "Line 1"),
            UserEntity(4, "qc_inspector", "Fatima Begum", UserRole.QC, "Quality Control"),
            UserEntity(5, "viewer", "Sarah Jenkins", UserRole.VIEWER, "Merchandising / Buyer Rep")
        )
    }

    fun getInitialStyles(): List<StyleEntity> {
        return listOf(
            StyleEntity(1, "ST-POLO-802", "H&M", "Men's Pique Polo Shirt", 14.50, 25000),
            StyleEntity(2, "ST-TSHIRT-104", "Zara", "Crew Neck Graphic Tee", 9.20, 40000),
            StyleEntity(3, "ST-DENIM-501", "Levi's", "Slim Fit 5-Pocket Jeans", 22.80, 15000),
            StyleEntity(4, "ST-HOODIE-920", "Target", "Fleece Pullover Hoodie", 18.30, 12000)
        )
    }

    fun getInitialPOs(): List<POEntity> {
        return listOf(
            POEntity(1, "PO-88210", "ST-POLO-802", "H&M", 10000, "Navy Blue", "M", "2026-09-20"),
            POEntity(2, "PO-88211", "ST-POLO-802", "H&M", 8000, "White", "L", "2026-09-22"),
            POEntity(3, "PO-99432", "ST-TSHIRT-104", "Zara", 20000, "Heather Grey", "S/M/L", "2026-09-18"),
            POEntity(4, "PO-77102", "ST-DENIM-501", "Levi's", 15000, "Indigo Wash", "32/32", "2026-09-30")
        )
    }

    fun getInitialInputReceives(todayDate: String): List<InputReceiveEntity> {
        return listOf(
            // Total input exactly 10,000 as per prompt example!
            InputReceiveEntity(
                id = 1,
                date = todayDate,
                buyer = "H&M",
                styleNo = "ST-POLO-802",
                poNo = "PO-88210",
                color = "Navy Blue",
                size = "M",
                lineNo = "Line 1",
                cuttingInputQty = 4000,
                receivedQty = 3800,
                previousBalance = 2200,
                totalInput = 6000,
                receiverName = "Rahim Ahmed (Prod Officer)",
                notes = "Batch 1 cutting received complete"
            ),
            InputReceiveEntity(
                id = 2,
                date = todayDate,
                buyer = "H&M",
                styleNo = "ST-POLO-802",
                poNo = "PO-88210",
                color = "Navy Blue",
                size = "L",
                lineNo = "Line 2",
                cuttingInputQty = 2500,
                receivedQty = 2500,
                previousBalance = 1500,
                totalInput = 4000,
                receiverName = "Rahim Ahmed (Prod Officer)",
                notes = "Batch 2 cutting received complete"
            )
        )
    }

    fun getInitialProductionOutputs(todayDate: String): List<ProductionOutputEntity> {
        // Line 1 & Line 2 outputs totaling:
        // Good Output ~ 7,500, Alter 150, Reject 50 => Total Output = 7,500
        // WIP/Balance = 10,000 - 7,500 = 2,300 (or Total Input 10,000 - 7,700 floor out = 2,300)
        val hours = listOf(
            Triple(1, "Hour 1 (08:00 - 09:00)", 140 to 135),
            Triple(2, "Hour 2 (09:00 - 10:00)", 160 to 158),
            Triple(3, "Hour 3 (10:00 - 11:00)", 160 to 162),
            Triple(4, "Hour 4 (11:00 - 12:00)", 160 to 155),
            Triple(5, "Hour 5 (13:00 - 14:00)", 160 to 150),
            Triple(6, "Hour 6 (14:00 - 15:00)", 160 to 164),
            Triple(7, "Hour 7 (15:00 - 16:00)", 160 to 158),
            Triple(8, "Hour 8 (16:00 - 17:00)", 160 to 160)
        )

        val list = mutableListOf<ProductionOutputEntity>()
        var id = 1L

        // Line 1 entries (Style ST-POLO-802)
        val line1Alters = listOf(15, 12, 18, 10, 14, 16, 11, 14) // Total ~110
        val line1Rejects = listOf(4, 3, 5, 2, 4, 3, 2, 5)        // Total ~28
        for (i in hours.indices) {
            val h = hours[i]
            val actual = 560 + (i * 12) % 35
            val alt = line1Alters[i]
            val rej = line1Rejects[i]
            val good = actual - alt - rej
            val target = 580
            list.add(
                ProductionOutputEntity(
                    id = id++,
                    date = todayDate,
                    lineNo = "Line 1",
                    styleNo = "ST-POLO-802",
                    poNo = "PO-88210",
                    hourLabel = h.second,
                    hourNumber = h.first,
                    hourlyTarget = target,
                    actualOutput = actual,
                    alterQty = alt,
                    rejectQty = rej,
                    goodOutput = good,
                    achievementPercent = (good.toDouble() / target.toDouble()) * 100.0,
                    enteredBy = "Tariqul Islam (Line Chief)",
                    qcInspector = "Fatima Begum (QC)"
                )
            )
        }

        // Line 2 entries
        val line2Alters = listOf(6, 4, 7, 5, 6, 4, 5, 3) // Total ~40 -> Total Alter = 110 + 40 = 150
        val line2Rejects = listOf(3, 2, 4, 3, 2, 3, 3, 2) // Total ~22 -> Total Reject = 28 + 22 = 50
        for (i in hours.indices) {
            val h = hours[i]
            val actual = 390 + (i * 9) % 25
            val alt = line2Alters[i]
            val rej = line2Rejects[i]
            val good = actual - alt - rej
            val target = 400
            list.add(
                ProductionOutputEntity(
                    id = id++,
                    date = todayDate,
                    lineNo = "Line 2",
                    styleNo = "ST-POLO-802",
                    poNo = "PO-88210",
                    hourLabel = h.second,
                    hourNumber = h.first,
                    hourlyTarget = target,
                    actualOutput = actual,
                    alterQty = alt,
                    rejectQty = rej,
                    goodOutput = good,
                    achievementPercent = (good.toDouble() / target.toDouble()) * 100.0,
                    enteredBy = "Karim Uddin (Line Chief)",
                    qcInspector = "Fatima Begum (QC)"
                )
            )
        }

        return list
    }

    fun getInitialLineBalance(): LineBalanceEntity {
        return LineBalanceEntity(
            id = 1,
            lineNo = "Line 1",
            styleNo = "ST-POLO-802",
            targetPerHour = 160,
            pitchTimeMinutes = 0.65,
            totalManpower = 24,
            totalSMV = 14.50,
            lineEfficiencyPercent = 82.5
        )
    }

    fun getInitialOperations(): List<OperationEntity> {
        // Detailed Polo Shirt operations showing realistic SMV, manpower, machine types and bottleneck
        return listOf(
            OperationEntity(
                id = 1,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 1,
                operationName = "Collar Make & Band Stitch",
                smv = 1.30,
                manpower = 2,
                machineType = "SNLS (Lockstitch)",
                targetPerHour = 160,
                actualPerHour = 162,
                efficiencyPercent = 87.7,
                cycleTimeMinutes = 0.65,
                isBottleneck = false,
                bottleneckReason = "Operating smoothly within pitch time",
                recommendation = "Maintain current operator allocation"
            ),
            OperationEntity(
                id = 2,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 2,
                operationName = "Placket Cut & Box Attach",
                smv = 1.85,
                manpower = 2,
                machineType = "SNLS (Automatic Box)",
                targetPerHour = 160,
                actualPerHour = 130,
                efficiencyPercent = 67.0,
                cycleTimeMinutes = 0.925, // 1.85 / 2 = 0.925 > 0.65 Pitch Time! BOTTLENECK!
                isBottleneck = true,
                bottleneckReason = "Cycle time (0.93 min) significantly exceeds line pitch time (0.65 min)",
                recommendation = "🔴 CRITICAL BOTTLENECK: Allocate +1 helper or split placket box stitch to reduce cycle time to 0.62 min"
            ),
            OperationEntity(
                id = 3,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 3,
                operationName = "Shoulder Join & Reinforce Tape",
                smv = 0.60,
                manpower = 1,
                machineType = "4-Thread Overlock",
                targetPerHour = 160,
                actualPerHour = 165,
                efficiencyPercent = 82.5,
                cycleTimeMinutes = 0.60,
                isBottleneck = false,
                bottleneckReason = "Balanced",
                recommendation = "Good flow"
            ),
            OperationEntity(
                id = 4,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 4,
                operationName = "Collar Join to Neckline",
                smv = 1.25,
                manpower = 2,
                machineType = "SNLS (Lockstitch)",
                targetPerHour = 160,
                actualPerHour = 158,
                efficiencyPercent = 82.3,
                cycleTimeMinutes = 0.625,
                isBottleneck = false,
                bottleneckReason = "Near pitch time threshold",
                recommendation = "Provide pre-trimmed collar pieces to prevent delays"
            ),
            OperationEntity(
                id = 5,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 5,
                operationName = "Back Neck Tape Attach",
                smv = 0.70,
                manpower = 1,
                machineType = "Flatlock (Interlock)",
                targetPerHour = 160,
                actualPerHour = 152,
                efficiencyPercent = 75.0,
                cycleTimeMinutes = 0.70,
                isBottleneck = false,
                bottleneckReason = "Slight WIP accumulation before tape attach",
                recommendation = "Use tape folder guide attachment"
            ),
            OperationEntity(
                id = 6,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 6,
                operationName = "Sleeve Rib Hemming & Join",
                smv = 1.15,
                manpower = 2,
                machineType = "4-Thread Overlock",
                targetPerHour = 160,
                actualPerHour = 164,
                efficiencyPercent = 78.5,
                cycleTimeMinutes = 0.575,
                isBottleneck = false,
                bottleneckReason = "Buffer available",
                recommendation = "Well balanced"
            ),
            OperationEntity(
                id = 7,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 7,
                operationName = "Side Seam Close & Vent Tack",
                smv = 1.40,
                manpower = 2,
                machineType = "4-Thread Overlock",
                targetPerHour = 160,
                actualPerHour = 156,
                efficiencyPercent = 75.8,
                cycleTimeMinutes = 0.70,
                isBottleneck = false,
                bottleneckReason = "Side vent requires accurate bar-tacking",
                recommendation = "Ensure bar tack machine is adjacent to overlock"
            ),
            OperationEntity(
                id = 8,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 8,
                operationName = "Bottom Hemming",
                smv = 0.85,
                manpower = 1,
                machineType = "Flatlock (Coverstitch)",
                targetPerHour = 160,
                actualPerHour = 160,
                efficiencyPercent = 90.7,
                cycleTimeMinutes = 0.85,
                isBottleneck = false,
                bottleneckReason = "High operator skill grade (A+)",
                recommendation = "Operator is meeting target consistently"
            ),
            OperationEntity(
                id = 9,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 9,
                operationName = "Button Hole & Button Stitch",
                smv = 0.90,
                manpower = 1,
                machineType = "Electronic Button Attacher",
                targetPerHour = 160,
                actualPerHour = 159,
                efficiencyPercent = 79.5,
                cycleTimeMinutes = 0.90,
                isBottleneck = false,
                bottleneckReason = "Machine automatic cycle time",
                recommendation = "Verify needle thread tension regularly"
            ),
            OperationEntity(
                id = 10,
                lineNo = "Line 1",
                styleNo = "ST-POLO-802",
                sequence = 10,
                operationName = "Thread Trimming & QC Inspection",
                smv = 0.95,
                manpower = 2,
                machineType = "Manual Inspection Table",
                targetPerHour = 160,
                actualPerHour = 162,
                efficiencyPercent = 80.2,
                cycleTimeMinutes = 0.475,
                isBottleneck = false,
                bottleneckReason = "Smooth final line exit",
                recommendation = "Immediate tagging of alters back to line"
            )
        )
    }

    fun getInitialManpower(): List<ManpowerEntity> {
        return listOf(
            ManpowerEntity(1, "OP-101", "Nazrul Islam", "Line 1", "A+", "Collar Make & Band Stitch", "SNLS", 80, true),
            ManpowerEntity(2, "OP-102", "Salma Begum", "Line 1", "A", "Collar Make & Band Stitch", "SNLS", 80, true),
            ManpowerEntity(3, "OP-103", "Kabir Hossain", "Line 1", "B", "Placket Cut & Box Attach", "SNLS Box", 65, true),
            ManpowerEntity(4, "OP-104", "Rashid Mia", "Line 1", "B", "Placket Cut & Box Attach", "SNLS Box", 65, true),
            ManpowerEntity(5, "OP-105", "Shirin Akhter", "Line 1", "A", "Shoulder Join & Tape", "4-Thread O/L", 160, true),
            ManpowerEntity(6, "OP-106", "Rina Paul", "Line 1", "A+", "Collar Join to Neckline", "SNLS", 80, true),
            ManpowerEntity(7, "OP-107", "Jamal Khan", "Line 1", "A", "Collar Join to Neckline", "SNLS", 80, true),
            ManpowerEntity(8, "OP-108", "Mizanur Rahman", "Line 1", "A", "Back Neck Tape Attach", "Flatlock", 160, true),
            ManpowerEntity(9, "OP-109", "Monira Khatun", "Line 1", "B+", "Sleeve Rib Hemming", "4-Thread O/L", 80, true),
            ManpowerEntity(10, "OP-110", "Anowar Hossain", "Line 1", "B", "Sleeve Rib Hemming", "4-Thread O/L", 80, true),
            ManpowerEntity(11, "OP-111", "Faruk Ahmed", "Line 1", "A", "Side Seam Close & Vent", "4-Thread O/L", 80, true),
            ManpowerEntity(12, "OP-112", "Shahidul Islam", "Line 1", "A", "Side Seam Close & Vent", "4-Thread O/L", 80, true),
            ManpowerEntity(13, "OP-113", "Babul Mia", "Line 1", "A+", "Bottom Hemming", "Flatlock", 160, true),
            ManpowerEntity(14, "OP-114", "Nurjahan Bibi", "Line 1", "A", "Button Hole & Stitch", "Auto Button", 160, true),
            ManpowerEntity(15, "OP-115", "Rehana Sultana", "Line 1", "A", "QC Inspection Table", "Manual", 160, true),
            // Line 2 sample operators
            ManpowerEntity(16, "OP-201", "Al-Amin", "Line 2", "A", "Collar Make", "SNLS", 75, true),
            ManpowerEntity(17, "OP-202", "Habiba Akter", "Line 2", "A", "Shoulder Join", "4-Thread O/L", 150, true),
            ManpowerEntity(18, "OP-203", "Sajib Hasan", "Line 2", "B", "Side Seam", "4-Thread O/L", 75, false) // 1 absent for realistic attendance
        )
    }
}
