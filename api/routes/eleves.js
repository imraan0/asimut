/**
 * @fileoverview Routes CRUD des élèves
 * @module routes/eleves
 */

const express = require('express');
const router = express.Router();
const eleveController = require('../controllers/eleveController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');
const upload = require('../middlewares/upload');

router.post('/import', verifyToken, checkRole('secretariat'), upload.single('fichier'), eleveController.importCSV);
router.get('/',     verifyToken, checkRole('secretariat', 'proviseur', 'professeur'), eleveController.getAll);
router.get('/:id',  verifyToken, checkRole('secretariat', 'proviseur', 'professeur'), eleveController.getById);
router.post('/',    verifyToken, checkRole('secretariat'), eleveController.create);
router.put('/:id',  verifyToken, checkRole('secretariat'), eleveController.update);
router.delete('/:id', verifyToken, checkRole('secretariat'), eleveController.remove);

module.exports = router;