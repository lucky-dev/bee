package bee.lang.ast;

public enum AccessModifier {

    PRIVATE(0),
    PROTECTED(1),
    PUBLIC(2);

    private int mLevelOfAccess;

    AccessModifier(int levelOfAccess) {
        mLevelOfAccess = levelOfAccess;
    }

    public boolean isWeakerThan(AccessModifier accessModifier) {
        return mLevelOfAccess < accessModifier.mLevelOfAccess;
    }

}
