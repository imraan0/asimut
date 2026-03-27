/**
 * @fileoverview Routes des conventions de stage
 * @module routes/conventions
 */

const express = require('express');
const router = express.Router();
const conventionController = require('../controllers/conventionController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.put('/:id/valider', verifyToken, checkRole('professeur'),                                      conventionController.valider);
router.get('/:id/pdf',     verifyToken, checkRole('eleve', 'professeur', 'secretariat', 'proviseur'), conventionController.generatePdf);
router.get('/:id',         verifyToken, checkRole('eleve', 'professeur', 'secretariat', 'proviseur'), conventionController.getById);
router.post('/',           verifyToken, checkRole('eleve'),                                           conventionController.create);

module.exports = router;