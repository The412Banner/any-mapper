package any.mapper.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SourceType { BUTTON, AXIS_POS, AXIS_NEG, AXIS_FULL }
enum class TargetType { KEY, KEY_COMBO, MOUSE_LEFT, MOUSE_RIGHT, MOUSE_MIDDLE, MOUSE_MOVE_X, MOUSE_MOVE_Y, TEXT }
enum class CurveType { LINEAR, QUADRATIC, CUBIC }
enum class HoldType { HELD, TOGGLE, SINGLE_SHOT }

@Entity(
    tableName = "mappings",
    foreignKeys = [ForeignKey(
        entity = Profile::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId")]
)
data class Mapping(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val label: String = "",
    val sourceType: SourceType = SourceType.BUTTON,
    val sourceCode: Int = 0,
    val deviceDescriptor: String? = null,
    val targetType: TargetType = TargetType.KEY,
    val targetKeyCode: Int = 0,
    val targetModifiers: Int = 0,
    val deadZone: Float = 0.15f,
    val sensitivity: Float = 1.0f,
    val curve: CurveType = CurveType.LINEAR,
    val holdBehavior: HoldType = HoldType.HELD,
    val enabled: Boolean = true
)
