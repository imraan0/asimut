const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Classe = sequelize.define('classe', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    lettre: {
        type: DataTypes.STRING(2),
        allowNull: false
    },
    annee_scolaire: {
        type: DataTypes.STRING(9),
        allowNull: false
    }
}, {
    timestamps: false,
    underscored: true
})

module.exports = Classe