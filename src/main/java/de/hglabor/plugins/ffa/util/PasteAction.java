package de.hglabor.plugins.ffa.util;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PasteAction implements IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException> {
    private final File schematicFile;
    private final BlockVector3 to;

    public PasteAction(File schematicFile, Location to) {
        this.to = BukkitAdapter.asBlockVector(to);
        this.schematicFile = schematicFile;
    }

	public Integer execute(ICancelabeEditSession editSession) {
		try {
			FileInputStream stream = new FileInputStream(schematicFile);
			ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(stream);

			Clipboard clipboard = reader.read();
			ClipboardHolder holder  = new ClipboardHolder(clipboard);

			final Operation operation = holder
					.createPaste(editSession)
					.to(to)
					.copyBiomes(true)
					.ignoreAirBlocks(false)
					.build();
			Operations.complete(operation);
		} catch (IOException | WorldEditException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
