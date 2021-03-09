package bee.lang.translate;

import bee.lang.ir.tree.IRStatement;
import bee.lang.translate.frame.Frame;

public class ProcedureFragment extends Fragment {

    private IRStatement mBody;
    private Frame mFrame;

    public ProcedureFragment(IRStatement body, Frame frame) {
        mBody = body;
        mFrame = frame;
    }

    public IRStatement getBody() {
        return mBody;
    }

    public Frame getFrame() {
        return mFrame;
    }

}
