var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
var TICK = ASMAPI.mapMethod("func_73660_a");

 function patch(method, name, patchFunction)
 {
	if (method.name != name) {
		return false;
	}
	print("[MineTogether ServerLoginNetHandler Transformer]: Attempting to patch method: " + name + " (" + method.name + ")");
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"MineTogether ServerLoginNetHandler Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.network.login.ServerLoginNetHandler"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;

				for (var i in methods) {
					if (patch(methods[i], TICK, patchTick)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchTick(instructions)
{
	var loginTimeout;

	for (var i = 0; i < instructions.size(); i++) {
		var instruction = instructions.get(i);

		if (instruction.getOpcode() == Opcodes.SIPUSH && instruction.operand == 600) {
			loginTimeout = instruction;
			break;
		}
	}

	instructions.insert(loginTimeout, 5000);
	instructions.remove(loginTimeout);
}