/**
 * @fileoverview Configuration de multer pour l'upload de fichiers CSV
 * @module middlewares/upload
 */

const multer = require('multer');
const path = require('path');

/**
 * Configuration du stockage — multer garde le fichier
 * temporairement dans le dossier uploads/
 */
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');  // dossier de stockage temporaire
  },
  filename: (req, file, cb) => {
    // nom unique pour éviter les conflits : timestamp + nom original
    cb(null, `${Date.now()}-${file.originalname}`);
  }
});

/**
 * Filtre — on accepte uniquement les fichiers CSV
 */
const fileFilter = (req, file, cb) => {
  const ext = path.extname(file.originalname).toLowerCase();
  if (ext !== '.csv') {
    return cb(new Error('Seuls les fichiers CSV sont acceptés'));
  }
  cb(null, true);
};

const upload = multer({ storage, fileFilter });

module.exports = upload;