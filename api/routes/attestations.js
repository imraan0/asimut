/**
 * @fileoverview Routes CRUD des attestations
 * @module routes/attestations
 */

const express = require('express');
const router = express.Router();
const attestationController = require('../controllers/attestationController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');
const { uploadPdf } = require('../middlewares/upload');

router.get('/:id/pdf',    verifyToken, checkRole('eleve', 'professeur', 'secretariat', 'proviseur'), attestationController.generatePdf);
router.get('/:id',        verifyToken, checkRole('eleve', 'professeur', 'secretariat', 'proviseur'), attestationController.getById);
router.put('/:id/upload', verifyToken, checkRole('eleve', 'secretariat'), uploadPdf.single('pdf'),      attestationController.upload);

module.exports = router;