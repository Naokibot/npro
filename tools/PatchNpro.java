import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PatchNpro {

    private static final String NPRO_PLUGIN = "com/sagakenichi/npro/NproPlugin";

    private PatchNpro() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("usage: PatchNpro <input.class> <output.class>");
        }

        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        ClassReader reader = new ClassReader(Files.readAllBytes(input));
        ClassWriter writer = new ClassWriter(0);

        reader.accept(new NproClassVisitor(writer), 0);
        Files.write(output, writer.toByteArray());
    }

    private static final class NproClassVisitor extends ClassVisitor {

        private NproClassVisitor(ClassWriter writer) {
            super(Opcodes.ASM8, writer);
        }

        @Override
        public MethodVisitor visitMethod(
                int access,
                String name,
                String descriptor,
                String signature,
                String[] exceptions
        ) {
            MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (!name.equals("onEnable") || !descriptor.equals("()V")) {
                return method;
            }
            return new EnableMethodVisitor(method);
        }

        @Override
        public void visitEnd() {
            addDataAccessor();
            addDataFileAccessor();
            super.visitEnd();
        }

        private void addDataAccessor() {
            MethodVisitor method = super.visitMethod(
                    Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                    "dailyRewardData",
                    "()Lorg/bukkit/configuration/file/YamlConfiguration;",
                    null,
                    null
            );
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitFieldInsn(
                    Opcodes.GETFIELD,
                    NPRO_PLUGIN,
                    "data",
                    "Lorg/bukkit/configuration/file/YamlConfiguration;"
            );
            method.visitInsn(Opcodes.ARETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        private void addDataFileAccessor() {
            MethodVisitor method = super.visitMethod(
                    Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                    "dailyRewardDataFile",
                    "()Ljava/io/File;",
                    null,
                    null
            );
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitFieldInsn(Opcodes.GETFIELD, NPRO_PLUGIN, "dataFile", "Ljava/io/File;");
            method.visitInsn(Opcodes.ARETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }
    }

    private static final class EnableMethodVisitor extends MethodVisitor {

        private EnableMethodVisitor(MethodVisitor method) {
            super(Opcodes.ASM8, method);
        }

        @Override
        public void visitLdcInsn(Object value) {
            super.visitLdcInsn("Npro 1.1.0 enabled.".equals(value) ? "Npro 1.2.1 enabled." : value);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/sagakenichi/npro/DailyLoginRewardListener",
                        "register",
                        "(Lcom/sagakenichi/npro/NproPlugin;)V",
                        false
                );
            }
            super.visitInsn(opcode);
        }
    }
}
