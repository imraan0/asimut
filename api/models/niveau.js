const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Niveau = sequelize.define('niveau', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    numero: {
        type: DataTypes.INTEGER,
        allowNull: false,
        unique: true
    }
}, {
    timestamps: false,
    underscored: true
})

module.exports = Niveau