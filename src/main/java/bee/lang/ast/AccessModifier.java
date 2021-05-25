package bee.lang.ast;

public enum AccessModifier {

    NONE(0),
    PRIVATE(1),
    PROTECTED(2),
    PUBLIC(3);

    private int mLevelOfAccess;

    AccessModifier(int levelOfAccess) {
        mLevelOfAccess = levelOfAccess;
    }

    public boolean isWeakerThan(AccessModifier accessModifier) {
        return mLevelOfAccess < accessModifier.mLevelOfAccess;
    }

}
