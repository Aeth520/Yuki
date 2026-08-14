package cn.aetheris.yuki.check;

/**
 * Which processing pipeline a check belongs to.
 * DEFAULT means the pipeline is inferred from the check's superclass.
 */
public enum CheckPipeline {
    DEFAULT,
    PACKET,
    PRE_VIA_PACKET,
    PRE_PREDICTION,
    PRE_VIA_POST_PREDICTION,
    POST_PREDICTION
}