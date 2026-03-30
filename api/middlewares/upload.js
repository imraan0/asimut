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

/**
 * Filtre — on accepte uniquement les fichiers PDF
 */
const pdfFilter = (req, file, cb) => {
  const ext = path.extname(file.originalname).toLowerCase();
  if (ext !== '.pdf') {
    return cb(new Error('Seuls les fichiers PDF sont acceptés'));
  }
  cb(null, true);
};

const uploadPdf = multer({
  storage: multer.diskStorage({
    destination: (req, file, cb) => {
      cb(null, 'uploads/attestations/');
    },
    filename: (req, file, cb) => {
      cb(null, `${Date.now()}-${file.originalname}`);
    }
  }),
  fileFilter: pdfFilter
});

module.exports = { upload, uploadPdf };