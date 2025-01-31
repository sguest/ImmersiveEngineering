package blusunrize.immersiveengineering.common.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class TemplateChunk extends LevelChunk
{
	private final Predicate<BlockPos> shouldShow;
	private final Holder<Biome> biome;

	public TemplateChunk(Level worldIn, ChunkPos chunkPos, List<StructureBlockInfo> blocksInChunk, Predicate<BlockPos> shouldShow)
	{
		super(worldIn, chunkPos);
		Registry<Biome> biomeRegistry = worldIn.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		for(int i = 0; i < getSections().length; ++i)
			getSections()[i] = new TemplateChunkSection(i, biomeRegistry, shouldShow);
		this.shouldShow = shouldShow;
		this.biome = worldIn.getUncachedNoiseBiome(0, 0, 0);
		for(StructureBlockInfo info : blocksInChunk)
		{
			actuallSetBlockState(info.pos, info.state);
			if(info.nbt!=null)
			{
				BlockEntity tile = BlockEntity.loadStatic(info.pos, info.state, info.nbt);
				if(tile!=null)
				{
					tile.setLevel(worldIn);
					getBlockEntities().put(info.pos, tile);
				}
			}
		}
	}

	@Nonnull
	@Override
	public FluidState getFluidState(@Nonnull BlockPos pos)
	{
		return getBlockState(pos).getFluidState();
	}


	@Nullable
	@Override
	public BlockEntity getBlockEntity(@Nonnull BlockPos pos, @Nonnull EntityCreationType creationMode)
	{
		if(!shouldShow.test(pos))
			return null;
		return getBlockEntities().get(pos);
	}

	@Nullable
	@Override
	public BlockState setBlockState(@Nonnull BlockPos pos, @Nonnull BlockState state, boolean isMoving)
	{
		return null;
	}

	public void actuallSetBlockState(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		final int sectionIndex = getSectionIndex(pos.getY());
		final int sectionX = pos.getX()&15;
		final int sectionY = pos.getY()&15;
		final int sectionZ = pos.getZ()&15;
		final TemplateChunkSection section = (TemplateChunkSection)getSection(sectionIndex);
		section.actuallySetBlockState(sectionX, sectionY, sectionZ, state);
	}

	@Override
	public int getLightEmission(@Nonnull BlockPos pos)
	{
		return 0;
	}

	@Override
	public void addAndRegisterBlockEntity(@Nonnull BlockEntity blockEntity)
	{
	}

	@Override
	public void setBlockEntity(@Nonnull BlockEntity blockEntity)
	{
	}

	@Override
	public void removeBlockEntity(@Nonnull BlockPos pos)
	{
	}

	// Not always correct, but hopefully "good enough"
	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public boolean isYSpaceEmpty(int startY, int endY)
	{
		return false;
	}

	@Nonnull
	@Override
	public ChunkHolder.FullChunkStatus getFullStatus()
	{
		return ChunkHolder.FullChunkStatus.BORDER;
	}

	@Nonnull
	@Override
	public Holder<Biome> getNoiseBiome(int p_204426_, int p_204427_, int p_204428_)
	{
		return this.biome;
	}
}
