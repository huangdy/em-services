package com.leidos.xchangecore.core.em.util;

import java.io.OutputStream;
import java.util.Map;

import org.springframework.web.servlet.View;

import com.usersmarts.xmf2.MarshalContext;

/**
 * DynamicXmfView
 *
 * @author Patrick Neal - Image Matters, LLC
 * @package com.usersmarts.cx.web
 * @created Dec 4, 2008
 */
public class XmfView
implements View {

    private String contentType = "text/xml";

    private MarshalContext marshalContext;

    public XmfView() {

    }

    public String getContentType() {

        return contentType;
    }

    public MarshalContext getMarshalContext() {

        return marshalContext;
    }

    @SuppressWarnings("unchecked")
    public void render(Map model, HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        Object output = model.get("output");
        if (output != null) {
            String format = contentType;
            if (format != null && format.contains("atom") && format.contains("xml")) {
                // IE contentType bug fix
                format = "text/xml";
            }
            response.setContentType(format);
            OutputStream out = response.getOutputStream();
            marshalContext.marshal(output, out);
        }
    }

    @Override
    public void render(Map arg0, HttpServletRequest arg1, HttpServletResponse arg2)
        throws Exception {

        // TODO Auto-generated method stub

    }

    public void setContentType(String contentType) {

        contentType = contentType;
    }

    public void setMarshalContext(MarshalContext marshalContext) {

        marshalContext = marshalContext;
    }
}
