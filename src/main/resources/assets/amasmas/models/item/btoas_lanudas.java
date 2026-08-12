// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "tu_mod_id:item/botas_lanudas"
  }
}
public class CustomModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "custommodel"), "main");
	private final ModelPart Botaizq;
	private final ModelPart Botader;

	public CustomModel(ModelPart root) {
		this.Botaizq = root.getChild("Botaizq");
		this.Botader = root.getChild("Botader");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Botaizq = partdefinition.addOrReplaceChild("Botaizq", CubeListBuilder.create().texOffs(1, 0).addBox(-1.0F, 6.0F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition Botader = partdefinition.addOrReplaceChild("Botader", CubeListBuilder.create().texOffs(2, 12).addBox(-2.2F, 6.0F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Botaizq.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Botader.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}